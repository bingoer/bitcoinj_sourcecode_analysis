/*
 * Copyright 2016 Robin Owens
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

import java.net.InetAddress;

import org.bitcoinj.core.FullPrunedBlockChain;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.FullPrunedBlockStore;
import org.bitcoinj.store.LevelDBFullPrunedBlockStore;

/**
 * bingoer
 * 2018-03-30
 */

/**
 * 该类主要测试从主网下载区块，下载到第39w个区块为止.
 * 主要涉及到BlockStore存储模块、BlockChain链模块、PeerGroup节点组模块。
 *  bitocinj的存储有很多种方式,LevelDBFullPrunedBlockStore只是其中一种使用leveldb存储到数据库中的方式
 *
 * */
public class LevelDB {
	public static void main(String[] args) throws Exception {
		/*
		 * This is just a test runner that will download blockchain till block
		 * 390000 then exit.
		 */
		//初始化LevelDBFullPrunedBlockStore对象，arg[0]是filename.
		//LevelDBFullPrunedBlockStore implements FullPrunedBlockStore，其中
		//FullPrunedBlockStore extends BlockStore, UTXOProvider
		FullPrunedBlockStore store = new LevelDBFullPrunedBlockStore(
				MainNetParams.get(), args[0], 1000, 100 * 1024 * 1024l,
				10 * 1024 * 1024, 100000, true,
				390000);

		//初始化FullPrunedBlockChain ，FullPrunedBlockChain extends AbstractBlockChain
		FullPrunedBlockChain vChain = new FullPrunedBlockChain(
				MainNetParams.get(), store);
		//设置是否运行脚本（比如从节点接收数的时候，需要运行脚本验证交易，如果节点可靠，可以设置为false。如果不可靠可以设置为true）
		vChain.setRunScripts(false);

		//初始化PeerGroup
		PeerGroup vPeerGroup = new PeerGroup(MainNetParams.get(), vChain);
		//设置是否使用本地节点：如果本地有bitcoin节点，那么就单独连接该节点，而不是去使用P2p
		vPeerGroup.setUseLocalhostPeerWhenPossible(true);
		vPeerGroup.addAddress(InetAddress.getLocalHost());

		vPeerGroup.start();
		//开始下载区块
		vPeerGroup.downloadBlockChain();
	}
}
