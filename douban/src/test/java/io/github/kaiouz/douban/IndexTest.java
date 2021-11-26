package io.github.kaiouz.douban;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import org.junit.Test;

import java.util.List;

public class IndexTest {


    @Test
    public void token() {
        String text = "王小波经典中篇小说《绿毛水怪》将改编电影。《绿毛水怪》是王小波早期手稿作品，以天马行空的想象，极具魔幻色彩的情感脉络，独树一帜的批评、反讽，受到广大书迷的喜爱。王小波曾创作电影剧本《东宫西宫》，此后尚未有作品改编成电影。据悉，李银河将担任《绿毛水怪》电影版的文学顾问。";
        List<Term> terms = IndexTokenizer.segment(text);
        terms.stream().filter(it ->it.word.length() > 1 && it.nature.firstChar() == 'n').forEach(System.out::println);
    }

}
