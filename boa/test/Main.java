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
			Scanner sc=new Scanner(System.in);
			int x=sc.nextInt();
			x=90;
			for(int i=0;i<2;i++) {
				System.out.println(compute(x));	
			}
	}
	
	public static void maincopy(String[] a) {		
			Scanner sc=new Scanner(System.in);
			int y=90;
			int j=0;
			while(j<2) {
				System.out.println(compute(y));
				j++;				
			}
	}
}
