package gdev.gbalancer;

import gdev.gen.tree.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Esta clase representa las combinaciones de los grupos en forma de árbol.
 * Mezcla todas las posibles combinaciones de grupos para obtener distintos formularios. 
 * Puntuando estos formularios y quedandose con el de mejor puntuación obtiene
 *  la mejor combinación posible de los grupos de un formulario.
 *  @author Dynagent
 *
 */
public class GGrpCombinationTree extends GGenericTree
{
    public GGrpCombinationTree()
    {
    }
    public GGrpCombinationTree(GNode root)
    {
        super(root);
    }
    public void addData(int level,Object data)
    {
        Vector vNodes = getNodeList(level-1);
        Enumeration enumNodes = vNodes.elements();
        while(enumNodes.hasMoreElements())
        {
            GNode oneNode = (GNode)enumNodes.nextElement();
            oneNode.addChild(new GNode(data));
        }
    }
    public Vector getNodeList(int level)
    {
        Vector v = new Vector();
        GNode root = getRoot();
        addNodeToVector(v,root,level);
        return v;
    }
    protected int addNodeToVector(Vector v,GNode oneNode,int level)
    {
        int theLevel = oneNode.getLevel();
        if(theLevel>level)
            return -1;
        if(theLevel==level)
        {
            v.addElement(oneNode);
            return 0;
        }
        Vector vChildren = oneNode.getChildren();
        if(vChildren!=null)
        {
            Enumeration enumChildren = vChildren.elements();
            while (enumChildren.hasMoreElements())
            {
                GNode oneChild = (GNode) enumChildren.nextElement();
                addNodeToVector(v, oneChild, level);
                //if(addNodeToVector(v, oneChild, level) == -1)
                //    return -1;
            }
        }
        return 0;
    }
    public Enumeration enumPaths()
    {
        return new GGCPathEnumeration(this);
    }
    void print()
    {
        printNode(getRoot());
    }
    void printNode(GNode oneNode)
    {
        //System.outprintln("level="+oneNode.getLevel()+" "+oneNode);
        Vector vChildren = oneNode.getChildren();
        if(vChildren!=null)
        {
            Enumeration enumChildren = vChildren.elements();
            while (enumChildren.hasMoreElements())
            {
                GNode oneChild = (GNode) enumChildren.nextElement();
                printNode(oneChild);
            }
        }
    }

    public static void main(String[] args)
    {
        GGrpCombinationTree tree = new GGrpCombinationTree(new GNode());
        tree.addData(1, new Integer(10));
        tree.addData(1, new Integer(11));

        tree.addData(2, new Integer(20));
        tree.addData(2, new Integer(21));

        tree.addData(3, new Integer(30));
        tree.addData(3, new Integer(31));
/*
        tree.print();

        Vector vL = tree.getNodeList(2);
        Enumeration e = vL.elements();
        while(e.hasMoreElements())
        {
            System.out.println(e.nextElement());
        }
*/

        Enumeration enO = tree.enumPaths();
        while (enO.hasMoreElements())
        {
            Vector v = (Vector) enO.nextElement();
            Enumeration enI = v.elements();
            while (enI.hasMoreElements())
            {
                GNode oneNode = (GNode) enI.nextElement();
                Integer data = (Integer) oneNode.getData();
                //System.outprintln("Val=" + data.intValue());
            }
        }
    }
}

class GGCPathEnumeration implements Enumeration
{
    Vector m_vPaths=new Vector();
    Enumeration m_enumPaths;

    public GGCPathEnumeration(GGenericTree tree)
    {
//        Vector vec = new Vector();
        GNode root = tree.getRoot();
//        vec.addElement(root);
//        calculatePath(root,vec);
        calculatePath(root,null);
        m_enumPaths = m_vPaths.elements();
    }
    private void calculatePath(GNode node,Vector vParents)
    {
        Vector vNew = null;
        if (node.getLevel() > 0)
        {
            if (vParents != null)
                vNew = new Vector(vParents);
            else
                vNew = new Vector();
            vNew.addElement(node);
        }
        if(node.isLeaf())
        {
            if(node.getLevel()>0)
                m_vPaths.addElement(vNew);
        }
        else
        {
            Vector vChildren = node.getChildren();
            if(vChildren!=null)
            {
                Enumeration enumChild = vChildren.elements();
                while (enumChild.hasMoreElements())
                {
                    GNode oneChildNode = (GNode) enumChild.nextElement();
                    Vector vOneChild = null;
                    if (vNew != null)
                        vOneChild = new Vector(vNew);
                    else
                        vOneChild = new Vector();
                    //vOneChild.addElement(oneChildNode);
                    calculatePath(oneChildNode, vOneChild);
                }
            }
        }
    }
    public boolean hasMoreElements()
    {
        return m_enumPaths.hasMoreElements();
    }
    public Object nextElement()
    {
        return m_enumPaths.nextElement();
    }
}
