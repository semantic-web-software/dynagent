package dynagent.common.communication;

import org.jdom.Element;

/**
 * Esta clase se encarga de la asignación del nivel propio de la accion, pudiendo ser preasignado, 
 * apropiado o lanzado.
 */

public class owningAction extends flowAction{
    public static final int OWNING_PREASIGNED=1;
    public static final int OWNING_APROPIATED=2;
    public static final int OWNING_RELEASED=3;

    private int owningLevel = 0;

    public Object clone(){
        owningAction act= new owningAction();
        cloneData(act);
        cloneFlowData(act);
        act.setOwningLevel(getOwningLevel());
        return act;
    }

    public boolean equals(Object obB){
        if( !(obB instanceof owningAction) || !super.equals(obB))
            return false;
        owningAction acB=(owningAction)obB;
        return  owningLevel==acB.getOwningLevel();
    }

    public int getOwningLevel() {
        return owningLevel;
    }

    public void setOwningLevel(int owningLevel) {
        this.owningLevel = owningLevel;
    }

    public owningAction(){
        super( message.MSG_OWNING );
    }

    public owningAction( flowAction fa, int orderType ){
        super( message.MSG_OWNING );
        fa.cloneData(this);
        fa.cloneFlowData(this);
        setType(message.MSG_OWNING);
        setOrderType(orderType);
    }

    void toElementHeader(Element root){
        super.toElementHeader(root);
        root.setAttribute("OW_LEVEL",String.valueOf(owningLevel));
    }

    void toElementContent( Element root ){
        super.toElementContent(root);
    }
}
