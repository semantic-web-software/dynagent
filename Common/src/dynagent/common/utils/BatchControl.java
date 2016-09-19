package dynagent.common.utils;

import java.util.ArrayList;
import java.util.HashMap;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class BatchControl implements IBatchListener {

	private ArrayList<HashMap<Integer, Integer>> sources;
	
	private IKnowledgeBaseInfo kb;
	
	public IKnowledgeBaseInfo getKb() {
		return kb;
	}

	public BatchControl(IKnowledgeBaseInfo kb){
		this.kb=kb;
		kb.addBatchListener(this);
	}
	
	@Override
	public void setSources(ArrayList<HashMap<Integer,Integer>> sources) {
		this.sources=sources;
	}

	@Override
	public ArrayList<HashMap<Integer,Integer>> getSources() {
		return sources;
	}

	@Override
	protected void finalize() throws Throwable {
		if(!kb.isDispose())
			kb.removeBatchListener(this);
		super.finalize();
	}
	
	

}
