package com.memcached;

import java.net.InetSocketAddress;

import net.spy.memcached.MemcachedClient;


public class MemcachedJava {

	public static void main(String[] args) {
		try {
			MemcachedClient mcc = new MemcachedClient(new InetSocketAddress("127.0.0.1",11211));
			System.out.println("memcached");
			mcc.shutdown();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

}
