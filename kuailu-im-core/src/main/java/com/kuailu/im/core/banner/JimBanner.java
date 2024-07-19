package com.kuailu.im.core.banner;

import com.kuailu.im.core.ImConst;
import com.kuailu.im.core.JimVersion;

import java.io.PrintStream;

/**
 * @author WChao
 * @Desc
 * @date 2020-05-02 01:12
 */
public class JimBanner implements Banner, ImConst {

    private static final String BANNER = " ";

    private static final String JIM = " :: "+ImConst.KIM +" :: ";

    @Override
    public void printBanner(PrintStream printStream) {
        printStream.println(BANNER);
        String version  = " (" + JimVersion.version + ")";
        printStream.println(JIM+version+"\n");
    }

}
