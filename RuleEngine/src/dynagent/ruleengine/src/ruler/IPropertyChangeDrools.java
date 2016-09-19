package dynagent.ruleengine.src.ruler;

import java.beans.PropertyChangeListener;

import org.drools.FactHandle;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public interface IPropertyChangeDrools {
	public FactHandle getFactHandle();

	public void setFactHandle(FactHandle factHandle);

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);
	
	public void removePropertyChangeListeners();
	
	public Object clone(IKnowledgeBaseInfo ik);
}
