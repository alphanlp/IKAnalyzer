package org.wltea.analyzer.sample;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * 从Demo测试结果来看，IK基本是为了索引分词来写的。
 * 原因有二：（1）没有词性标注；（2）停用词过滤。
 * 注意：存在一个小bug。待分词文本超过4096时，在4096附近，会分错。
 * 这是由于分词器，是按照字符数组缓冲来进行分词的，缓冲大小为4096，
 * 并且作者没有做相应处理，简单讲缓冲末尾加入了切分结果中，而
 * 没有进一步处理。因此在业务处理中，如待处理文本过长，需要自行断句。
 * 否则，会有错误出现。
 * @author User
 *
 */
public class IKSegmenterDemo {
	
	public static void main(String[] args) {
		String text = "这是由于分词器，是按照字符数组缓冲来进行分词的，缓冲大小为";// 当将缓冲字符数组调整为19时，很能反映类注释中的问题。
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
