/*
 * Copyright by the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.bitcoinj.examples;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.KeyChainEventListener;
import org.bitcoinj.wallet.listeners.ScriptsChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;

import java.io.File;
import java.util.List;

import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;

/**
 * The following example shows how to use the by bitcoinj provided WalletAppKit.
 * The WalletAppKit class wraps the boilerplate (Peers, BlockChain, BlockStorage, Wallet) needed to set up a new SPV
 * bitcoinj app.
 * <p>
 * In this example we also define a WalletEventListener class with implementors that are called when the wallet changes
 * (for example sending/receiving money)
 * 这是一个使用WalletAppkit的demo，walletAppKit包装了包括peers,blockchain，blockStorage和wallet在内的模块，是spv钱包中很重要的部分。
 * 流程为：1、设置网络。一般我们先在本地用bitcoind -regtest命令启动bitcoind，配置成本地的回归测试网络。回归网一般用于开发。
 * 					 testnet测试网和比特主网功能上区别基本一样，只是测试币无价值。
 * 		  2.初始化WalletAppKit，这个对象可以配置好相关环境，让我们直接开始使用wallet
 * 		  3.同步blockchain
 * 		  4.为钱包设置监听器（发送、接收交易和钱包变化等）。
 * 		  5.可以发送交易了
 */

/**
 * bingoer
 * 2018-03-30
 * */
public class Kit {

	public static void main(String[] args) {

		// First we configure the network we want to use.
		// The available options are:
		// - MainNetParams
		// - TestNet3Params
		// - RegTestParams
		// While developing your application you probably want to use the Regtest mode and run your local bitcoin
		// network. Run bitcoind with the -regtest flag
		// To test you app with a real network you can use the testnet. The testnet is an alternative bitcoin network
		// that follows the same rules as main network. Coins are worth nothing and you can get coins for example from
		// http://faucet.xeno-genesis.com/
		//
		// For more information have a look at: https://bitcoinj.github.io/testing and https://bitcoin
		// .org/en/developer-examples#testing-applications

		//Test
		//配置网络，使用testnet测试网
		NetworkParameters params = TestNet3Params.get();

		// Now we initialize a new WalletAppKit. The kit handles all the boilerplate for us and is the easiest way to
		// get everything up and running.
		// Have a look at the WalletAppKit documentation and its source to understand what's happening behind the
		// scenes: https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/kits/WalletAppKit
		// .java
		//初始化WalletAppKit对象
		WalletAppKit kit = new WalletAppKit(params, new File("."), "walletappkit-example");

		// In case you want to connect with your local bitcoind tell the kit to connect to localhost.
		// You must do that in reg test mode.
		//kit.connectToLocalHost();

		// Now we start the kit and sync the blockchain.
		// bitcoinj is working a lot with the Google Guava libraries. The WalletAppKit extends the AbstractIdleService
		// . Have a look at the introduction to Guava services: https://github.com/google/guava/wiki/ServiceExplained
		//同步区块
		kit.startAsync();
		kit.awaitRunning();

		//设置接收Coins的监听器
		kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
			@Override
			public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
				System.out.println("-----> coins resceived: " + tx.getHashAsString());
				System.out.println("received: " + tx.getValue(wallet));
			}
		});
		//设置发送coins的监听器
		kit.wallet().addCoinsSentEventListener(new WalletCoinsSentEventListener() {
			@Override
			public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
				System.out.println("coins sent");
			}
		});

		//keyChain：是钱包中的各种密钥工具。涉及到加解密和签名验签
		kit.wallet().addKeyChainEventListener(new KeyChainEventListener() {
			@Override
			public void onKeysAdded(List<ECKey> keys) {
				System.out.println("new key added");
			}
		});

		//脚本： 比特币中交易的发送和接收是需要有脚本来配合完成的，
		kit.wallet().addScriptsChangeEventListener(new ScriptsChangeEventListener() {
			@Override
			public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
				System.out.println("new script added");
			}
		});

		//交易的置信度改变
		kit.wallet().addTransactionConfidenceEventListener(new TransactionConfidenceEventListener() {
			@Override
			public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
				System.out.println("-----> confidence changed: " + tx.getHashAsString());
				TransactionConfidence confidence = tx.getConfidence();
				System.out.println("new block depth: " + confidence.getDepthInBlocks());
			}
		});

		// Ready to run. The kit syncs the blockchain and our wallet event listener gets notified when something
		// happens.
		// To test everything we create and print a fresh receiving address. Send some coins to that address and see
		// if everything works.
		//生成新地址
		System.out.println("send money to: " + kit.wallet().freshReceiveAddress().toString());


		//可以新增关闭同步区块等代码
		// Make sure to properly shut down all the running services when you manually want to stop the kit. The
		// WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our
		// application is stopping.
		//System.out.println("shutting down again");
		//kit.stopAsync();
		//kit.awaitTerminated();
	}

}
