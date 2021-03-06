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

import static io.github.azige.whitespace.text.Token.Type.*;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.azige.whitespace.command.Command;
import io.github.azige.whitespace.command.CommandFactory;

/**
 * 默认的语法解析器实现，此类使用{@link Tokenizer}将输入源转换为记号流，再将记号构造为指令。
 *
 * @author Azige
 */
public class DefaultParser implements Parser{

    private final Tokenizer tokenizer;
    private final CommandFactory cf;
    private final Map<Token.Type, Supplier<Command>> simpleCommandMap;
    private final Map<Token.Type, Function<BigInteger, Command>> numberParamCommandMap;
    private final Map<Token.Type, Function<String, Command>> labelParamCommandMap;

    /**
     * 用指定的输入源和指令工厂构造对象，将使用默认的词法分析器。
     *
     * @param input 输入源
     * @param commandFactory 指令工厂
     */
    public DefaultParser(Reader input, CommandFactory commandFactory){
        this(new TokenizerImpl(input), commandFactory);
    }

    /**
     * 用指定的词法分析器和指令工厂构造对象。
     *
     * @param tokenizer 词法分析器
     * @param commandFactory 指令工厂
     */
    public DefaultParser(Tokenizer tokenizer, CommandFactory commandFactory){
        this.cf = commandFactory;
        this.tokenizer = tokenizer;
        simpleCommandMap = buildSimpleCommandMap();
        numberParamCommandMap = buildNumberParamCommandMap();
        labelParamCommandMap = buildLabelParamCommandMap();
    }

    private Map<Token.Type, Supplier<Command>> buildSimpleCommandMap(){
        Map<Token.Type, Supplier<Command>> map = new HashMap<>();

        map.put(S_DUP, cf::dup);
        map.put(S_SWAP, cf::swap);
        map.put(S_DISCARD, cf::discard);
        map.put(A_ADD, cf::add);
        map.put(A_SUB, cf::sub);
        map.put(A_MUL, cf::mul);
        map.put(A_DIV, cf::div);
        map.put(A_MOD, cf::mod);
        map.put(H_STORE, cf::store);
        map.put(H_RETRIEVE, cf::retrieve);
        map.put(F_RETURN, cf::ret);
        map.put(F_EXIT, cf::exit);
        map.put(I_PCHAR, cf::printChar);
        map.put(I_PNUM, cf::printNumber);
        map.put(I_RCHAR, cf::readChar);
        map.put(I_RNUM, cf::readNumber);

        return Collections.unmodifiableMap(map);
    }

    private static Function<BigInteger, Command> wrapFunc(Function<Integer, Command> func){
        return n -> func.apply(n.intValue());
    }

    private Map<Token.Type, Function<BigInteger, Command>> buildNumberParamCommandMap(){
        Map<Token.Type, Function<BigInteger, Command>> map = new HashMap<>();

        map.put(S_PUSH, cf::push);
        map.put(S_DUP2, wrapFunc(cf::dup));
        map.put(S_REMOVE, wrapFunc(cf::slide));

        return map;
    }

    private Map<Token.Type, Function<String, Command>> buildLabelParamCommandMap(){
        Map<Token.Type, Function<String, Command>> map = new HashMap<>();

        map.put(F_MARK, cf::mark);
        map.put(F_CALL, cf::call);
        map.put(F_JUMP, cf::jump);
        map.put(F_JUMPZ, cf::jumpIfZero);
        map.put(F_JUMPN, cf::jumpIfNegative);

        return map;
    }

    /**
     * 获得下一条指令。
     *
     * @return 下一条指令，如果已经到达流末尾则为null
     */
    @Override
    public Command next(){
        Token token = tokenizer.next();
        if (token == null){
            return null;
        }

        if (simpleCommandMap.containsKey(token.getType())){
            return simpleCommandMap.get(token.getType()).get();
        }else if (numberParamCommandMap.containsKey(token.getType())){
            Token param = tokenizer.next();
            assert param.getType() == NUMBER;
            return numberParamCommandMap.get(token.getType()).apply(param.getNumber());
            // }else if (labelParamCommandMap.containsKey(token.getType())) // assert true
        }else{
            Token param = tokenizer.next();
            assert param.getType() == LABEL;
            return labelParamCommandMap.get(token.getType()).apply(param.getText());
        }
    }

    @Override
    public void close() throws IOException{
        tokenizer.close();
    }
}
