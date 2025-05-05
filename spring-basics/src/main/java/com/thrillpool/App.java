package com.thrillpool;

import com.thrillpool.springbeanssimple.GettingABean;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        new GettingABean().getThatBean();
        new GettingABean().getThatBeanPackageScan();
    }
}
