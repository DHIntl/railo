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
package railo.transformer.bytecode.literal;

import org.objectweb.asm.Type;

import railo.runtime.type.scope.Scope;
import railo.transformer.bytecode.BytecodeContext;
import railo.transformer.bytecode.BytecodeException;
import railo.transformer.bytecode.Position;
import railo.transformer.bytecode.expression.ExpressionBase;
import railo.transformer.bytecode.expression.var.DataMember;
import railo.transformer.bytecode.expression.var.Variable;
import railo.transformer.bytecode.util.ASMConstants;
import railo.transformer.bytecode.util.Types;

public class Null extends ExpressionBase  {


	public Null(Position start, Position end) {
		super(start, end);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {
		ASMConstants.NULL(bc.getAdapter());
		return Types.OBJECT;
	}

	public Variable toVariable() {
		Variable v = new Variable(Scope.SCOPE_UNDEFINED,getStart(),getEnd());
		v.addMember(new DataMember(LitString.toExprString("null")));
		return v;
	}

}
