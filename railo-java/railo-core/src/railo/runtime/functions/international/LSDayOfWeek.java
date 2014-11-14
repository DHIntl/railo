/**
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
/**
 * Implements the CFML Function dayofweek
 */
package railo.runtime.functions.international;

import java.util.Locale;
import java.util.TimeZone;

import railo.commons.date.DateTimeUtil;
import railo.commons.date.TimeZoneUtil;
import railo.runtime.PageContext;
import railo.runtime.exp.ExpressionException;
import railo.runtime.exp.PageException;
import railo.runtime.functions.BIF;
import railo.runtime.i18n.LocaleFactory;
import railo.runtime.op.Caster;
import railo.runtime.type.dt.DateTime;

public final class LSDayOfWeek extends BIF {
	
	private static final long serialVersionUID = -9002250869621547151L;

	public static double call(PageContext pc , DateTime date) {
		return _call(pc, date, pc.getLocale(),pc.getTimeZone());
	}
	
	public static double call(PageContext pc , DateTime date, String strLocale) throws ExpressionException {
		return _call(pc, date, LocaleFactory.getLocale(strLocale),pc.getTimeZone());
	}
	
	public static double call(PageContext pc , DateTime date, String strLocale, String strTimezone) throws ExpressionException {
		return _call(pc, date, 
				strLocale==null?pc.getLocale():LocaleFactory.getLocale(strLocale), 
				strTimezone==null?pc.getTimeZone():TimeZoneUtil.toTimeZone(strTimezone));
	}
	
	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==1)return call(pc,Caster.toDatetime(args[0],pc.getTimeZone()));
		if(args.length==2)return call(pc,Caster.toDatetime(args[0],pc.getTimeZone()),Caster.toString(args[1]));
		return call(pc,Caster.toDatetime(args[0],pc.getTimeZone()),Caster.toString(args[1]),Caster.toString(args[2]));
	}

	private static double _call(PageContext pc , DateTime date,Locale locale,TimeZone tz) {
		return DateTimeUtil.getInstance().getDayOfWeek(locale,tz, date);
	}
}