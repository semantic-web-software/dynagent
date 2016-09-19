package miniCalendar;

public abstract class DateRangeListener implements IDateListener{

	protected JCalendarRange calendarRange;
    protected boolean datePrimarySelectioned;
    protected boolean dateSecondarySelectioned;

    public DateRangeListener(JCalendarRange calendarRange){
    	this.calendarRange=calendarRange;
		datePrimarySelectioned=false;
		dateSecondarySelectioned=false;
	}
    
    protected void closeDialog(){
    	if(calendarRange.getDialog()!=null)
			calendarRange.getDialog().dispose();
		else System.err.println("WARNING: No hay un dialog asociado. El calendario no puede ser cerrado");
    }
	
}
