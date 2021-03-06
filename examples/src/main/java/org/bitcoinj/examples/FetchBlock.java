/*
 * Copyright 2011 Google Inc.
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

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Future;
/**
 * bingoer
 * 2018-03-31
 * */
/**
 * <p>Downloads the block given a block hash from the remote or localhost node and prints it out.</p>
 * <p>When downloading from localhost, run bitcoind locally: bitcoind -testnet -daemon.
 * After bitcoind is up and running, use command: org.bitcoinj.examples.FetchBlock --localhost &lt;blockHash&gt; </p>
 * <p>Otherwise, use command: org.bitcoinj.examples.FetchBlock &lt;blockHash&gt;, this command will download blocks from
 *a peer generated by DNS seeds.</p>
 */
//根据区块hash去下载区块的demo，有两种选项：1、从本地节点下载；2、从网络节点下载。
//如果从本地节点下载，现在本地使用 bitcoind -testnet -daemon命令启动本地节点（该demo使用的是测试网），然后用
// org.bitcoinj.examples.FetchBlock --localhost blockHash从本地开始下载
//如果不适用 --localhost参数，则默认从网络节点下载区块数据。
public class FetchBlock {
    public static void main(String[] args) throws Exception {
        BriefLogFormatter.init();
        // Parse command line arguments
        //解析命令行使用的是jopt-simple库
        OptionParser parser = new OptionParser();
        OptionSet opts = null;
        List<String> nonOpts = null;
        try {
            parser.accepts("localhost", "Connect to the localhost node");
            parser.accepts("help", "Displays program options");
            opts = parser.parse(args);
            if (opts.has("help")) {
                System.out.println("usage: org.bitcoinj.examples.FetchBlock [--localhost] <blockHash>");
                parser.printHelpOn(System.out);
                return;
            }
            nonOpts = opts.nonOptionArguments();
            //只支持下载单个区块的数据，不支持多区块一起下载
            if (nonOpts.size() != 1) {
                throw new IllegalArgumentException("Incorrect number of block hash, please provide only one block hash.");
            }
        } catch (OptionException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("usage: org.bitcoinj.examples.FetchBlock [--localhost] <blockHash>");
            parser.printHelpOn(System.err);
            return;
        }

        // Connect to testnet and find a peer
        System.out.println("Connecting to node");
        //返回测试网实例
        final NetworkParameters params = TestNet3Params.get();
        //使用的是加载到内存存储的方式
        BlockStore blockStore = new MemoryBlockStore(params);
        //BlockChain实例
        BlockChain chain = new BlockChain(params, blockStore);
        //PeerGroup实例
        PeerGroup peerGroup = new PeerGroup(params, chain);
        if (!opts.has("localhost")) {
            //用dns寻找peer
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        } else {
            //用给定的localhost地址去构建一个节点地址PeerAddress
            PeerAddress addr = new PeerAddress(params, InetAddress.getLocalHost());
            peerGroup.addAddress(addr);
        }
        peerGroup.start();
        //只要连接上一个节点就可以了
        peerGroup.waitForPeers(1).get();
        //获取节点，一般用dnsDiscovery发现的节点是一个list数组
        Peer peer = peerGroup.getConnectedPeers().get(0);

        // Retrieve a block through a peer
        Sha256Hash blockHash = Sha256Hash.wrap(nonOpts.get(0));
        //Future代表的是异步操作的结果， future.get()是真正的取值。
        Future<Block> future = peer.getBlock(blockHash);
        System.out.println("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        System.out.println(block);
        peerGroup.stopAsync();
    }
}
