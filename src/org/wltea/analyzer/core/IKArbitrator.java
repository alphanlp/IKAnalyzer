/**
 * IK 中文分词  版本 5.0
 * IK Analyzer release 5.0
 * 
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
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 * 
 */
package org.wltea.analyzer.core;

import java.util.Stack;
import java.util.TreeSet;

/**
 * IK分词歧义裁决器
 */
class IKArbitrator {

	IKArbitrator(){
		
	}
	
	/**
	 * 分词歧义处理
	 * @param orgLexemes
	 * @param useSmart
	 */
	
	void process(AnalyzeContext context , boolean useSmart){
		QuickSortSet orgLexemes = context.getOrgLexemes();
		Lexeme orgLexeme = orgLexemes.pollFirst();// 没有经过歧义处理
		
		LexemePath crossPath = new LexemePath();
		while(orgLexeme != null){
			//有歧义加入，没有歧义的情况下进行下面处理
			if(!crossPath.addCrossLexeme(orgLexeme)){
				//找到与crossPath不相交的下一个crossPath
				if(crossPath.size() == 1 || !useSmart){
					//crossPath没有歧义或者不做歧义处理，直接输出当前crossPath
					context.addLexemePath(crossPath);//添加到pathMap中
				}else{
					//对当前的crossPath进行歧义处理
					QuickSortSet.Cell headCell = crossPath.getHead();
					LexemePath judgeResult = this.judge(headCell, crossPath.getPathLength());
					//输出歧义处理结果judgeResult
					context.addLexemePath(judgeResult);
				}
				
				//把orgLexeme加入新的crossPath中
				crossPath = new LexemePath();
				crossPath.addCrossLexeme(orgLexeme);
			}
			
			orgLexeme = orgLexemes.pollFirst();
		}
		
		//处理最后的path
		if(crossPath.size() == 1 || !useSmart){
			//crossPath没有歧义或者不做歧义处理，直接输出当前crossPath
			context.addLexemePath(crossPath);
		}else{
			//对当前的crossPath进行歧义处理
			QuickSortSet.Cell headCell = crossPath.getHead();
			LexemePath judgeResult = this.judge(headCell, crossPath.getPathLength());
			//输出歧义处理结果judgeResult
			context.addLexemePath(judgeResult);
		}
	}
	
	/**
	 * 歧义识别。比如“国家人民”。
	 * @param lexemeCell 歧义路径链表头
	 * @param fullTextLength 歧义路径文本长度
	 * @param option 候选结果路径
	 * @return
	 */
	private LexemePath judge(QuickSortSet.Cell lexemeCell , int fullTextLength){
		//候选路径集合
		TreeSet<LexemePath> pathOptions = new TreeSet<LexemePath>();
		//候选结果路径
		LexemePath option = new LexemePath();
		
		//对crossPath进行一次遍历,同时返回本次遍历中有冲突的Lexeme栈
		Stack<QuickSortSet.Cell> lexemeStack = this.forwardPath(lexemeCell , option);
		
		//当前词元链并非最理想的，加入候选路径集合
		pathOptions.add(option.copy());
		
//		QuickSortSet.Cell temp= option.getHead();
//		while(temp != null){
//			System.out.println(temp.getLexeme());
//			temp = temp.getNext();
//		}
//		System.out.println();
		
		//存在歧义词，处理
		QuickSortSet.Cell c = null;
		while(!lexemeStack.isEmpty()){
			c = lexemeStack.pop();// 弹出一个链，比如“国家”
			
			//回滚词元链，直到没有歧义发生
			this.backPath(c.getLexeme(), option);
			
			//从歧义词位置开始，递归，生成候选方案
			this.forwardPath(c, option);
			
//			QuickSortSet.Cell temp1= option.getHead();
//			while(temp1 != null){
//				System.out.println(temp1.getLexeme());
//				temp1 = temp1.getNext();
//			}
//			System.out.println();
			
			pathOptions.add(option.copy());// 国家/人/民
		}
		
		//返回集合中的最优方案
		return pathOptions.first();
	}
	
	/**
	 * 向前遍历，添加词元，构造一个无歧义词元组合。
	 * 比如“国家人民”，option对应的的LexemePath为"国家/人民"，返回的conflictStack中存储“国，家，家人，人,民”5个为头的链
	 * @param LexemePath path
	 * @return
	 */
	private Stack<QuickSortSet.Cell> forwardPath(QuickSortSet.Cell lexemeCell , LexemePath option){
		//发生冲突的Lexeme栈
		Stack<QuickSortSet.Cell> conflictStack = new Stack<QuickSortSet.Cell>();
		QuickSortSet.Cell c = lexemeCell;
		//迭代遍历Lexeme链表
		while(c != null && c.getLexeme() != null){
			if(!option.addNotCrossLexeme(c.getLexeme())){// 当没有歧义加入，有交叉歧义时进行如下处理
				//词元交叉，添加失败则加入lexemeStack栈
				conflictStack.push(c);
			}
			c = c.getNext();
		}
		return conflictStack;
	}
	
	/**
	 * 回滚词元链，直到它能够接受指定的词元
	 * @param lexeme 
	 * @param l
	 */
	private void backPath(Lexeme l, LexemePath option){
		while(option.checkCross(l)){
			option.removeTail();
		}
	}
	
}
