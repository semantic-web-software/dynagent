package gdev.gen.tree;

import java.util.Enumeration;
import java.util.Vector;

public class GGenericBinaryTree
{
    protected GTwoChildNode m_objRoot;

    public GGenericBinaryTree()
    {
        setRoot(null);
    }

    public GGenericBinaryTree(Object o)
    {
        setRoot(new GTwoChildNode(o));
    }

    public GTwoChildNode getRoot()
    {
        return m_objRoot;
    }

    public void setRoot(GTwoChildNode r)
    {
        m_objRoot = r;
    }

    public boolean isEmpty()
    {
        return getRoot() == null;
    }

    public Object getData()
    {
        if (!isEmpty())
            return getRoot().getData();
        return null;
    }

    public GTwoChildNode getLeft()
    {
        if (!isEmpty())
            return getRoot().getLeft();
        return null;
    }

    public GTwoChildNode getRight()
    {
        if (!isEmpty())
            return getRoot().getRight();
        return null;
    }

    public void setData(Object o)
    {
        if (!isEmpty())
            getRoot().setData(o);
    }

    public void insertLeft(GTwoChildNode p, Object o)
    {
        if ( (p != null) && (p.getLeft() == null))
            p.setLeft(new GTwoChildNode(o));
    }

    public void insertRight(GTwoChildNode p, Object o)
    {
        if ( (p != null) && (p.getRight() == null))
            p.setRight(new GTwoChildNode(o));
    }
    public Enumeration enumPaths()
    {
        return new GGBTPathEnumeration(this);
    }
}

class GGBTPathEnumeration implements Enumeration
{
    Vector m_vPaths=new Vector();
    Enumeration m_enumPaths;

    public GGBTPathEnumeration(GGenericBinaryTree tree)
    {
        Vector vec = new Vector();
        GTwoChildNode root = tree.getRoot();
        vec.addElement(root);
        if(!root.isLeaf())
            calculatePath(root,vec);
        else
            m_vPaths.addElement(vec);
        m_enumPaths = m_vPaths.elements();
    }
    private void calculatePath(GTwoChildNode node,Vector vParents)
    {
        if(node.isLeaf())
        {
            Vector vNew = new Vector(vParents);
//            vNew.addElement(node);
            m_vPaths.addElement(vNew);
        }
        else
        {
            Vector vLeft = new Vector(vParents);
            GTwoChildNode nodeLeft = node.getLeft();
            vLeft.addElement(nodeLeft);
            calculatePath(nodeLeft,vLeft);
            Vector vRight = new Vector(vParents);
            GTwoChildNode nodeRight = node.getRight();
            vRight.addElement(nodeRight);
            calculatePath(nodeRight,vRight);
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
