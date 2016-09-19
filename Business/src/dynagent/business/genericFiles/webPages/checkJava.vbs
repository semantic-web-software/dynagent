/* Uso:
* JPlug.isEnabled() devuelve true en caso de detección positiva,
* false, en el caso contrario,
* o bien null si ninguna detección es posible.
*/
/* autor: diego barcia @ < http://www.javascripters.com.ar > 27.02.07 */


var JPlug = {
	MICROSOFT_VBCODE_HEAD : "<" + 'script language="VBscript"' + ">" + '\n on error resume next' +
	'\n VBScriptEngine = False' +
	'\n If ScriptEngineMajorVersion >= 2 then' +
	'\n VBScriptEngine = True' +
	'\n End If' +
	'\n' + "<" + '/scr' + 'ipt' + ">",
	MICROSOFT_VBCODE_BODY : "<" + 'script language="VBscript"' + ">" + 
	'\n If VBScriptEngine Then' + 
	'\n on error resume next' + 
	'\n CreateObject("JavaPlugin")' + 
	'\n If Err.Number <> 0 Then' +
	'\n MMSS_PLUGIN = false' +
	'\n Else' +
	'\n MMSS_PLUGIN = true' +
	'\n End If' +
	'\n End If' +
	"<" + '/scr' + 'ipt' + ">",
	filterMicrosoft : function ( )
	{
		if (navigator.userAgent.toLowerCase().indexOf('mac') == -1 && window.ActiveXObject ){
			document.write(JPlug.MICROSOFT_VBCODE_HEAD);
		}
	},
	detectMicrosoft : function ()
	{
		if (typeof window['VBScriptEngine'] != 'undefined' ) {
			document.write(JPlug.MICROSOFT_VBCODE_BODY);
		}
		if (typeof window['MMSS_PLUGIN'] != 'undefined') {
			return window['MMSS_PLUGIN'];
		}
		return null;
	},
	isEnabled : function () {
		JPlug.filterMicrosoft();
		return JPlug.detectMicrosoft();
		return null;
	}
};