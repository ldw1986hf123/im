package com.kuailu.im.server.service.impl;

import com.kuailu.im.server.service.FileService;
import com.kuailu.im.server.starter.BaseJunitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceImplTest extends BaseJunitTest {
    @Autowired
    FileService fileService;

    @Test
    void upload() {
    }

    @Test
    void escape() {
        String input = "This is a string 中文";
        String output = escape(input);
        System.out.println(output);
    }


    public   String escape(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (isChinese(ch)) {
                output.append("\\u" + Integer.toHexString(ch));
            } else {
                output.append(ch);
            }
        }

        return output.toString();
    }

    private static boolean isChinese(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }


}