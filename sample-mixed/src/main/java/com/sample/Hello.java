package com.sample;

class Hello {
    public static void main(String[] args) {
        String hello = Library.INSTANCE.getHello();
        System.out.println(hello);
    }
}
