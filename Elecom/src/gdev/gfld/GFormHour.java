package gdev.gfld;

import gdev.gen.GConfigView;
import gdev.gen.GConst;

import java.awt.Dimension;

public class GFormHour extends GFormField {

	public Dimension getMinimumComponentDimension() {
		if(!m_objViewBalancer.getFilterMode())
			return new Dimension((int)(GConfigView.hourWidth*1.3), (int)m_objViewBalancer.getRowHeight(isHighlighted()));
		else
			return new Dimension((int)((3)*GConfigView.hourWidth), (int)m_objViewBalancer.getRowHeight(isHighlighted()));
	}

	public int getType() {
		return GConst.TM_HOUR;
	}

}
