/*
 * Copyright 2011 Google Inc.
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

import java.io.File;

import org.bitcoinj.wallet.Wallet;

/**
 * 这是备份钱包的demo,但只实现了导入序列化后的钱包并输出序列化的内容
 * DumpWallet loads a serialized wallet and prints information about what it contains.
 * lkz
 *2018-04-4~2018-04-5
 */
public class DumpWallet {
    public static void main(String[] args) throws Exception {
        //args[0]  ：钱包文件的路径
        if (args.length != 1) {
            System.out.println("Usage: java DumpWallet <filename>");
            return;
        }

        Wallet wallet = Wallet.loadFromFile(new File(args[0]));
        System.out.println(wallet.toString());
    }
}
