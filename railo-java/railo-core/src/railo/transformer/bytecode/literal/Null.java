package railo.transformer.bytecode.literal;

import org.objectweb.asm.Type;

import railo.transformer.bytecode.BytecodeContext;
import railo.transformer.bytecode.BytecodeException;
import railo.transformer.bytecode.Literal;
import railo.transformer.bytecode.Position;
import railo.transformer.bytecode.expression.ExprInt;
import railo.transformer.bytecode.expression.ExpressionBase;
import railo.transformer.bytecode.util.ASMConstants;
import railo.transformer.bytecode.util.Types;
// NULL Support
public class Null extends ExpressionBase  {


	public Null(Position start, Position end) {
		super(start, end);
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {
		ASMConstants.NULL(bc.getAdapter());
		return Types.OBJECT;
	}

}
