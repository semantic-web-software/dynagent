package gdev.gen.tree;

import java.util.Enumeration;
import java.util.Vector;

public class GGenericTree
{
    protected GNode m_objRoot;

    public GGenericTree()
    {
    }
    public GGenericTree(GNode root)
    {
        m_objRoot = root;
    }
    public GNode getRoot()
    {
        return m_objRoot;
    }
    public void setRoot(GNode root)
    {
        m_objRoot = root;
    }
    public Enumeration enumPaths()
    {
        return new GGTPathEnumeration(this);
    }
}

class GGTPathEnumeration implements Enumeration
{
    Vector m_vPaths=new Vector();
    Enumeration m_enumPaths;

    public GGTPathEnumeration(GGenericTree tree)
    {
        Vector vec = new Vector();
        GNode root = tree.getRoot();
//        vec.addElement(root);
        calculatePath(root,vec);
        m_enumPaths = m_vPaths.elements();
    }
    private void calculatePath(GNode node,Vector vParents)
    {
        if(node.isLeaf())
        {
            Vector vNew = new Vector(vParents);
            vNew.addElement(node);
            m_vPaths.addElement(vNew);
        }
        else
        {
            Vector vChildren = node.getChildren();
            Enumeration enumChild = vChildren.elements();
            while(enumChild.hasMoreElements())
            {
                GNode oneChildNode = (GNode)enumChild.nextElement();
                Vector vOneChild = new Vector(vParents);
//                vOneChild.addElement(oneChildNode);
                calculatePath(oneChildNode, vOneChild);
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
