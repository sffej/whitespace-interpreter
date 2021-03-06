/*
 * Copyright 2014 Azige.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.azige.whitespace.text;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;

import io.github.azige.whitespace.command.Command;
import io.github.azige.whitespace.command.DefaultCommandFactory;

/**
 * 用于将源程序整个转换为伪代码的类。
 *
 * @author Azige
 */
public class PseudoCodeGenerator{

    private final PrintStream out;
    private final PseudoCodeFormatter formatter;

    /**
     * 以指定的输出流构造对象，使用默认的格式化器。
     *
     * @param out 输出流
     * @see PseudoCodeGenerator#PseudoCodeGenerator(OutputStream,
     * PseudoCodeFormatter)
     */
    public PseudoCodeGenerator(OutputStream out){
        this(out, new PseudoCodeFormatter());
    }

    /**
     * 以指定的输出流和格式化器构造对象。生成的伪代码都将输出到指定的输出流中。
     *
     * @param out 输出流
     * @param formatter 格式化器
     */
    public PseudoCodeGenerator(OutputStream out, PseudoCodeFormatter formatter){
        if (out instanceof PrintStream){
            this.out = (PrintStream)out;
        }else{
            this.out = new PrintStream(out);
        }
        this.formatter = formatter;
    }

    /**
     * 从指定的输入流读入源代码并生成伪代码，将使用默认的解析器。
     *
     * @param input 输入流
     */
    public void translate(Reader input){
        translate(new DefaultParser(input, new DefaultCommandFactory()));
    }

    /**
     * 从指定的解析器读入指令并生成伪代码。
     *
     * @param parser 解析器
     */
    public void translate(Parser parser){
        Command command;
        while ((command = parser.next()) != null){
            out.println(formatter.format(command));
        }
    }
}
