package boa.test;

import java.util.Scanner;

public class Main {
	
	public static int compute(int x) {
		if(x>100) {
			return x-10;
		}	
		else {
			return compute(compute(x+11));		
		}
	}	

	public static void main(String[] args) {
		try {		
			Scanner sc=new Scanner(System.in);
			int x=sc.nextInt();
			x=90;
			if(x>100) {
				System.out.println(91);		
			}	
			else {
				System.out.println(compute(x));		
			}
		}
		catch(Exception e) {
		}
	}
}
