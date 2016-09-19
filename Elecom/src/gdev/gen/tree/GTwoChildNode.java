package gdev.gen.tree;

import java.util.Vector;

public class GTwoChildNode
{
    protected Object m_objData;
    protected GTwoChildNode m_objParent;
    protected GTwoChildNode m_objChildLeft;
    protected GTwoChildNode m_objChildRight;

    public GTwoChildNode()
    {
        m_objData = null;
        m_objChildLeft = null;
        m_objChildRight = null;
        m_objParent = null;
    }

    public GTwoChildNode(Object d)
    {
        m_objData = d;
        m_objChildLeft = null;
        m_objChildRight = null;
        m_objParent = null;
    }

    public void setParent(GTwoChildNode parent)
    {
        m_objParent = parent;
    }

    public GTwoChildNode getParent()
    {
        return m_objParent;
    }

    public void setLeft(GTwoChildNode l)
    {
        m_objChildLeft = l;
        m_objChildLeft.setParent(this);
    }

    public void setRight(GTwoChildNode r)
    {
        m_objChildRight = r;
        m_objChildRight.setParent(this);
    }

    public void setData(Object d)
    {
        m_objData = d;
    }

    public GTwoChildNode getLeft()
    {
        return m_objChildLeft;
    }

    public GTwoChildNode getRight()
    {
        return m_objChildRight;
    }

    public Object getData()
    {
        return m_objData;
    }

    public boolean isLeaf()
    {
        if(m_objChildLeft!=null||m_objChildRight!=null)
            return false;
        return true;
    }
    public Vector getPath()
    {
        Vector v = new Vector();
//        v.addElement(this);
        GTwoChildNode parent = this.getParent();
        while(parent!=null)
        {
            v.add(parent);
            parent=parent.getParent();
        }
        return v;
    }
}
