package com.chamago.bison.stub;

/**
 * Created by pengrongxin on 2017/1/15.
 */
public class IUserServiceStub implements IUserService {
    public String testNetty(String message) {
        System.out.println("receive client request ..........");
        return message+" is come from server";


    }
}
