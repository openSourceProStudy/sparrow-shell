/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sparrow.cg;

import com.sparrow.cg.impl.DynamicCompiler;

import java.util.logging.Logger;

/**
 * @author harry
 */
public class CGTest {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        Printer printer = (Printer) DynamicCompiler
            .getInstance().sourceToObject("com.sparrow.cg.Demo1",
                "package com.sparrow.cg;\n" +
                    "\n" +
                    "/**\n" +
                    " * @author harry\n" +
                    " */\n" +
                    "public class Demo1 implements Printer {\n" +
                    "    public String print() {\n" +
                    "        return \"hello world22\";\n" +
                    "    }\n" +
                    "}");
        System.out.println(printer.print());
        DynamicCompiler.getInstance().unload(printer);
        ClassLoader classLoader=printer.getClass().getClassLoader();
        while (classLoader!=null) {
            System.out.println(classLoader);
            classLoader=classLoader.getParent();
        }
    }
}