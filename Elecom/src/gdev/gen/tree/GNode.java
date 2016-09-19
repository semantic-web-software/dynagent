package gdev.gen.tree;

import java.util.Vector;

public class GNode
{
    protected Object m_objData;
    protected Vector m_vChildren;
    protected int m_iLevel;

    public GNode()
    {
    }
    public GNode(Object obj)
    {
        m_objData = obj;
    }
    public void setData(Object obj)
    {
        m_objData = obj;
    }
    public Object getData()
    {
        return m_objData;
    }
    public Vector getChildren()
    {
        return m_vChildren;
    }
    public void addChild(GNode node)
    {
        if(m_vChildren==null)
            m_vChildren = new Vector();
        m_vChildren.addElement(node);
        node.m_iLevel = m_iLevel+1;
    }
    public int getLevel()
    {
        return m_iLevel;
    }
    public boolean isLeaf()
    {
        if(m_vChildren==null)
            return true;
        return false;
    }
}
