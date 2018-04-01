/*
 * Copyright 2012 Google Inc.
 * Copyright 2014 Andreas Schildbach
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
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import com.google.common.util.concurrent.ListenableFuture;

import java.net.InetAddress;
import java.util.List;
/**
 * bingoer
 * 2018-04-01
 * */
/**
 * Downloads the given transaction and its dependencies from a peers memory pool then prints them out.
 */
//FetchTransactions：整个demo的流程和FetchBlock类似。显示设置各种对象最终得到一个连接上的peer对象（由PeerGroup管理）
public class FetchTransactions {
    //args[0]:transaction的hash，String类型。
    public static void main(String[] args) throws Exception {
        BriefLogFormatter.init();
        System.out.println("Connecting to node");
        //设定网络,使用测试网
        final NetworkParameters params = TestNet3Params.get();
        //指定存储方法，实例化存储对象
        BlockStore blockStore = new MemoryBlockStore(params);
        //实例化BlockChain对象
        BlockChain chain = new BlockChain(params, blockStore);
        //实例化PeerGroup对象
        PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.start();
        //添加获取到的由本地地址构建的PeerAddress
        peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        //只要连接上的节点数有一个就够了,numPeers参数代表的是需要登录连接上的节点数
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        //提取txHash
        Sha256Hash txHash = Sha256Hash.wrap(args[0]);
        //Future返回的是一种操作的结果的对象。该实例中获取的是内存中的交易
        ListenableFuture<Transaction> future = peer.getPeerMempoolTransaction(txHash);
        System.out.println("Waiting for node to send us the requested transaction: " + txHash);
        //真正的获取future所对应的结果
        Transaction tx = future.get();
        System.out.println(tx);

        System.out.println("Waiting for node to send us the dependencies ...");
        //获取交易tx的相关依赖,返回的是List<Transaction>，这种依赖也是相关的交易。
        List<Transaction> deps = peer.downloadDependencies(tx).get();
        for (Transaction dep : deps) {
            System.out.println("Got dependency " + dep.getHashAsString());
        }

        System.out.println("Done.");
        peerGroup.stop();
    }
}
