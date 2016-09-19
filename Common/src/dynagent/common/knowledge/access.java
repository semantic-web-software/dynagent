package dynagent.common.knowledge;


import java.io.Serializable;

import dynagent.common.Constants;

public class access extends Object implements Serializable{
	protected 	boolean findAccess	= false;
	protected 	boolean newAccess 	= false;
	protected 	boolean delAccess 	= false;
	protected 	boolean viewAccess 	= false;
	protected 	boolean setAccess 	= false;
	protected 	boolean abstractAccess 	= false;
	
	

	public final static int OVER_PROPERTY=1;
	public final static int OVER_OBJECT=2;
	public final static int OVER_OBJECT_AND_PROPERTY=3;
		
	//PERMISOS VERSIÓN ACTUAL
	public final static int VIEW=Constants.ACCESS_VIEW;//dynagent.application.action.GET;
	public final static int NEW=Constants.ACCESS_NEW;//dynagent.application.action.NEW;
	public final static int SET=Constants.ACCESS_SET;//dynagent.application.action.SET;
	public final static int DEL=Constants.ACCESS_DEL;
    public final static int ABSTRACT=Constants.ACCESS_ABSTRACT;
    public final static int FIND=Constants.ACCESS_FIND;
    public final static int sumallaccess=VIEW+NEW+DEL+SET+ABSTRACT+FIND;
    


	protected int accessValue=0;

	protected access(){;}

	public access( int orders ){
		viewAccess= 	(orders & VIEW )>0;
		newAccess= 	(orders & NEW )>0;
		setAccess= 	(orders & SET )>0;
		findAccess= 	(orders & FIND )>0;
		delAccess= 	(orders & DEL )>0;
		abstractAccess=(orders & ABSTRACT)>0;
		accessValue=orders;
	}
	
	
	 public static access getAllAccess(){
	    	access acc=new access(access.sumallaccess);
	    	return acc;
	    	
	    }

	
/*
	public access( access myAccess, access rootAccess ){

		viewAccess= 	myAccess.viewAccess && rootAccess.viewAccess;

		newAccess= 	(	rootAccess.newAccess ||
					rootAccess.setAccess ) &&

					myAccess.newAccess;

		setAccess= 	(	rootAccess.newAccess ||
					rootAccess.setAccess ) &&

					myAccess.setAccess;

		relAccess= 	(	rootAccess.newAccess ||
					rootAccess.setAccess ) &&

					myAccess.relAccess;

		unrelAccess= 	(	rootAccess.newAccess ||
					rootAccess.setAccess ) &&

					myAccess.unrelAccess;

		delAccess= 	(	rootAccess.newAccess ||
					rootAccess.setAccess ) &&

					myAccess.delAccess;
		
		concreteAccess= myAccess.concreteAccess && rootAccess.concreteAccess;
		commentAccess= myAccess.commentAccess && rootAccess.commentAccess;
		specializeAccess= myAccess.specializeAccess && rootAccess.specialize;
		
	}
	*/
	
	
	

	public access(	boolean viewAccess,
			boolean newAccess,
			boolean setAccess,
			boolean delAccess,
			boolean findAccess,
			boolean abstractAccess){

		this.newAccess 	= newAccess;
		this.delAccess 	= delAccess;
		this.viewAccess = viewAccess;
		this.setAccess 	= setAccess;
		this.abstractAccess = abstractAccess;
		this.findAccess = findAccess;
		accessValue= 	(viewAccess 	? VIEW:0)+
				(newAccess 	? NEW:0)+
				(findAccess 	? FIND:0)+
				(setAccess 	? SET:0)+
				(delAccess 	? DEL:0)+
				(abstractAccess 	? ABSTRACT:0);
	}

        public int getOperation(){
            return accessValue;
        }

	public access( String str ){
		this( 	str!= null && str.indexOf("VIEW")>=0,
			str!= null && str.indexOf("NEW")>=0,
			str!= null && str.indexOf("SET")>=0,
			str!= null && str.indexOf("RREL")>=0,
			str!= null && str.indexOf("DEL")>=0,
			str!= null && str.indexOf("ABSTRACT")>=0);
	}

/*
	public access( access currentAccess, int rootApplicationOperation ){
		viewAccess= currentAccess.viewAccess;
		newAccess= 	currentAccess.newAccess &&
				rootApplicationOperation!=access.VIEW &&
				//rootApplicationOperation!=appControl.OBJ_SELECTION &&
				rootApplicationOperation!=access.DEL;
		relAccess= 	currentAccess.relAccess &&
				rootApplicationOperation!=access.VIEW &&
				rootApplicationOperation!=access.DEL;
		unrelAccess=	currentAccess.unrelAccess &&
				rootApplicationOperation!=access.VIEW &&
				rootApplicationOperation!=access.DEL;
		setAccess= 	currentAccess.setAccess &&
				rootApplicationOperation!=access.VIEW &&
				rootApplicationOperation!=access.DEL;
		delAccess= 	currentAccess.delAccess &&
				(rootApplicationOperation==access.DEL ||
                                 rootApplicationOperation==access.NEW ||
                                rootApplicationOperation==access.SET);
		concreteAccess= currentAccess.concreteAccess;
		commentAccess= currentAccess.commentAccess;
		specializeAccess= currentAccess.specializeAccess;
	}
	*/

	public boolean getViewAccess(){ return viewAccess; }
	public boolean getNewAccess(){ return newAccess; }
	public boolean getSetAccess(){ return setAccess; }
	public boolean getDelAccess(){ return delAccess; }
	public boolean getAbstractAccess(){ return abstractAccess; }
	public boolean getFindAccess() {return findAccess;	}


	public boolean matches( int data ){
		return (data & accessValue) >0;
	}
	
	
	
	public Object clone(){
		return new access( 	getViewAccess(),
					getNewAccess(),
					getSetAccess(),
					getDelAccess(),
					getFindAccess(),
					getAbstractAccess());
	}

	public String toString(){
		String res="";
		if( getViewAccess() ) res+="VIEW";
		if( getNewAccess() ){
			if( res.length()>0 ) res+=";";
			res+="NEW";
		}
		if( getSetAccess() ){
			if( res.length()>0 ) res+=";";
			res+="SET";
		}
		if( getDelAccess() ){
			if( res.length()>0 ) res+=";";
			res+="DEL";
		}
		if( getFindAccess() ){
			if( res.length()>0 ) res+=";";
			res+="FIND";
		}
		if( getAbstractAccess() ){
			if( res.length()>0 ) res+=";";
			res+="ABSTRACT";
		}
		return res;
	}
	

	public void setAbstractAccess(boolean abstractAccess) {
		this.abstractAccess = abstractAccess;
		boolean exist=this.matches(access.ABSTRACT);
		if((abstractAccess && !exist) || (!abstractAccess && exist))
			this.accessValue=abstractAccess?this.accessValue+access.ABSTRACT:this.accessValue-access.ABSTRACT;
	}
	
	public void setDelAccess(boolean delAccess) {
		this.delAccess = delAccess;
		boolean exist=this.matches(access.DEL);
		if((delAccess && !exist) || (!delAccess && exist))
			this.accessValue=delAccess?this.accessValue+access.DEL:this.accessValue-access.DEL;
	}
	
	public void setNewAccess(boolean newAccess) {
		this.newAccess = newAccess;
		boolean exist=this.matches(access.NEW);
		if((newAccess && !exist) || (!newAccess && exist))
			this.accessValue=newAccess?this.accessValue+access.NEW:this.accessValue-access.NEW;
	}
	
	public void setFindAccess(boolean findAccess) {
		this.findAccess = findAccess;
		boolean exist=this.matches(access.FIND);
		if((findAccess && !exist) || (!findAccess && exist))
			this.accessValue=findAccess?this.accessValue+access.FIND:this.accessValue-access.FIND;
	}
	
	public void setSetAccess(boolean setAccess) {
		this.setAccess = setAccess;
		boolean exist=this.matches(access.SET);
		if((setAccess && !exist) || (!setAccess && exist))
			this.accessValue=setAccess?this.accessValue+access.SET:this.accessValue-access.SET;
	}
	
	public void setViewAccess(boolean viewAccess) {
		this.viewAccess = viewAccess;
		boolean exist=this.matches(access.VIEW);
		if((viewAccess && !exist) || (!viewAccess && exist))
			this.accessValue=viewAccess?this.accessValue+access.VIEW:this.accessValue-access.VIEW;
	}
	
	public boolean equals(Object obj){
		if (obj instanceof access){
			access ac=(access) obj;
			if(ac.abstractAccess!=this.abstractAccess)
				return false;
			if(ac.accessValue!=this.accessValue)
				return false;
			if(ac.delAccess!=this.delAccess)
				return false;
			if(ac.newAccess!=this.newAccess)
				return false;
			if(ac.findAccess!=this.findAccess)
				return false;
			if(ac.setAccess!=this.setAccess)
				return false;
			if(ac.viewAccess!=this.viewAccess)
				return false;
			return true;
		}else{
			return false;
		}
	}

	}
