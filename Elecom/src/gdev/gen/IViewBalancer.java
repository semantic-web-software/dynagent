package gdev.gen;

import java.awt.Dimension;
import java.awt.Graphics;

/**
 * @author Dynagent
 *
 */
public interface IViewBalancer
{
    double getTextHeight(boolean highlighted);
    double getAveCharWidth(boolean highlighted);
    double getRowHeight(boolean highlighted);
    int getPanelLeftMargin();
    int getPanelTopMargin();
    int getPanelRightMargin();
    int getPanelBottomMargin();
    int getPanelHGap();
    int getPanelVGap();
    int getHCellPad();
    int getVCellPad();
    int getGroupTopMargin();
    int getGroupBottomMargin();
    int getGroupLeftMargin();
    int getGroupRightMargin();
    int getGroupHGap();
    int getGroupVGap();
    Dimension getDimString(String content,boolean bIsBold,boolean highlighted);
    boolean getFilterMode();
    int getAlignment();
    
    Dimension getPanelDimension();
    Dimension getPanelDimensionUseful();
    Graphics getGraphics();
}
