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
package railo.runtime.type.scope.storage.db;

import java.sql.SQLException;

import railo.commons.io.log.Log;
import railo.runtime.config.Config;
import railo.runtime.db.DatasourceConnection;
import railo.runtime.exp.PageException;
import railo.runtime.type.Query;
import railo.runtime.type.Struct;
import railo.runtime.type.scope.storage.StorageScopeEngine;
import railo.runtime.type.scope.storage.StorageScopeListener;
import railo.runtime.type.scope.storage.clean.DatasourceStorageScopeCleaner;

public class MySQL extends SQLExecutorSupport {

	@Override
	public Query select(Config config, String cfid, String applicationName,
			DatasourceConnection dc, int type, Log log,
			boolean createTableIfNotExist) throws PageException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Config config, String cfid, String applicationName,
			DatasourceConnection dc, int type, Struct data, long timeSpan,
			Log log) throws PageException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Config config, String cfid, String appName,
			DatasourceConnection dc, int type, Log log)
			throws PageException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void clean(Config config, DatasourceConnection dc, int type,
			StorageScopeEngine engine, DatasourceStorageScopeCleaner cleaner,
			StorageScopeListener listener, Log log) throws PageException,
			SQLException {
		// TODO Auto-generated method stub
		
	}

}
