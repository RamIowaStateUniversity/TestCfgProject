package boa.test.util;

public class ProcessData {
	public StringBuilder toHex(int s,int length){
		StringBuilder str = new StringBuilder();
		str.append(Integer.toHexString(s));
		while (str.length() < length) {
			str.insert(0, '0'); // pad with leading zero if needed
		}
		return str;
	}
	
	public StringBuilder toHex(long s,int length){
		StringBuilder str = new StringBuilder();
		str.append(Long.toHexString(s));
		while (str.length() < length) {
			str.insert(0, '0'); // pad with leading zero if needed
		}
		return str;
	}
	
	public String toBinary(int n) {
		if(n<0) {
			n=256+n;
		}
        if (n == 0) {
            return "0";
        }
        String binary = "";
        while (n > 0) {
            int rem = n % 2;
            binary = rem + binary;
            n = n / 2;
        }
        while((binary.length()-8)<0) {
        	binary="0"+binary;
        }
        return binary;
    }
}
