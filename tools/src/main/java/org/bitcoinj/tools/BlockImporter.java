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

package org.bitcoinj.tools;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.*;
import org.bitcoinj.utils.BlockFileLoader;
import com.google.common.base.Preconditions;

import java.io.File;

/**
 * Very thin wrapper around {@link BlockFileLoader}
 */
/**
 * 导入区块文件，通过调用BlockFileLoader实现。对该类的简单包装。
 * */
public class BlockImporter {
    public static void main(String[] args) throws BlockStoreException, VerificationException, PrunedException {
        System.out.println("USAGE: org.bitcoinj.tools.BlockImporter.BlockImporter (prod|test) (H2|Disk|MemFull|Mem|SPV) [blockStore]");
        System.out.println("       blockStore is required unless type is Mem or MemFull");
        System.out.println("       eg org.bitcoinj.tools.BlockImporter.BlockImporter prod H2 /home/user/bitcoinj.h2store");
        System.out.println("       Does full verification if the store supports it");
        Preconditions.checkArgument(args.length == 2 || args.length == 3);

        ////USAGE: org.bitcoinj.tools.BlockImporter.BlockImporter (prod|test) (H2|Disk|MemFull|Mem|SPV) [blockStore] 区块导入的命令格式
        NetworkParameters params;
        //选择主网或者测试网络。
        if (args[0].equals("test"))
            params = TestNet3Params.get();
        else
            params = MainNetParams.get();
        //BlockStore是一个接口，定义了一下存储block和设置/获取区块头的方法。
        BlockStore store;
        if (args[1].equals("H2")) {
            Preconditions.checkArgument(args.length == 3);
            //extends DatabaseFullPrunedBlockStore
            store = new H2FullPrunedBlockStore(params, args[2], 100);
        } else if (args[1].equals("MemFull")) {
            //实现了BlockStore, FullPrunedBlockStore接口，内存存储
            Preconditions.checkArgument(args.length == 2);
            store = new MemoryFullPrunedBlockStore(params, 100);
        } else if (args[1].equals("Mem")) {
            //实现BlockStore接口，内存储存，主要也用于单元测试
            Preconditions.checkArgument(args.length == 2);
            store = new MemoryBlockStore(params);
        } else if (args[1].equals("SPV")) {
            Preconditions.checkArgument(args.length == 3);
            //spv模式的内存存储。
            store = new SPVBlockStore(params, new File(args[2]));
        } else {
            System.err.println("Unknown store " + args[1]);
            return;
        }

        AbstractBlockChain chain = null;
        //FullPrunedBlockStore extends BlockStore, UTXOProvider
        //store是FullPrunedBlockStore的实例或者是它子类的实例
        //判断chain的类型是全节点类型还是spv
        if (store instanceof FullPrunedBlockStore)
            //全节点
            chain = new FullPrunedBlockChain(params, (FullPrunedBlockStore) store);
        else
            //chain的类型为spv
            chain = new BlockChain(params, store);
        //去取区块。
        BlockFileLoader loader = new BlockFileLoader(params, BlockFileLoader.getReferenceClientBlockFileList());

        //取回来block对象
        for (Block block : loader)
            chain.add(block);
    }
}
