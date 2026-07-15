package com.yasarbilgi.visitormeetingmanagment;

import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;

public class Test {
    public static void main(String[] args) {
        System.out.println(ErrorCode.ADMIN_CANNOT_MODIFY_ANOTHER_ADMIN.getMessageKey().toString());
    }
}
