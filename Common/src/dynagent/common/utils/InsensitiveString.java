package dynagent.common.utils;

public class InsensitiveString {
	private final String s;
	public InsensitiveString(String s) { this.s=s; }
	public String toString() { return s; }
	public boolean equals(Object obj) {
		boolean res=false;
		if(obj instanceof InsensitiveString){
			res= s.equalsIgnoreCase(((InsensitiveString) obj).toString());
			return res;
		}
		else{
			res= s.equalsIgnoreCase((String) obj);
		}
		return res;
		
	}
	public int compareTo(Object obj) {
		int res=0;
		if(obj instanceof InsensitiveString){
			res= s.compareToIgnoreCase(((InsensitiveString) obj).toString());
			return res;
		}
		else{
			res= s.compareToIgnoreCase((String) obj);
		}
		return res;		
	}
	public int hashCode(){
		if (s == null) return 0;

        int hash = 0;

        // This should end up more or less equal to input.toLowerCase().hashCode(), unless String
        // changes its implementation. Let's hope this is reasonably fast.

        for (int i = 0; i < s.length(); i++)
        {
            int ch = s.charAt(i);

            int caselessCh = Character.toLowerCase(ch);

            hash = 31 * hash + caselessCh;
        }

        return hash;
    
	}
}
