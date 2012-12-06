package railo.runtime.query.caster;

import java.io.IOException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArrayCast implements Cast {

	@Override
	public Object toCFType(int type, ResultSet rst, int columnIndex) throws SQLException, IOException {
		Array arr = rst.getArray(columnIndex);
		if(arr==null) return null;
		return arr.getArray();
	}

}
