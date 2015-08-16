package org.wltea.analyzer.sample;

import java.io.IOException;
import java.io.StringReader;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * 从Demo测试结果来看，IK基本是为了索引分词来写的。
 * 原因有二：（1）没有词性标注；（2）停用词过滤。
 * @author User
 *
 */
public class IKSegmenterDemo {
	
	public static void main(String[] args) {
		String text = "这是一个中文分词的例子，你可以直接运行它！IKAnalyer can analysis english text too.";
		IKSegmenter seg = new IKSegmenter(new StringReader(text),true);
		try {
			Lexeme lexeme = null;
			while((lexeme = seg.next()) != null){
				System.out.println(lexeme.getLexemeText());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
