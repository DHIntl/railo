<cfscript>
component extends="org.railo.cfml.test.RailoTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}
	public void function testArraySome() localMode="true" {
		_arraySome(false);
	}


	public void function testArraySomeParallel() localMode="true" {
		_arraySome(true);
	}

	private void function _arraySome(boolean parallel) localMode="true" {
		
		arr=['a','b','c'];
		//arr[5]='e';
		
		// base test
		res=ArraySome(arr, function(value ){return value =='b';},parallel);
		assertEquals(true,res);
		
		res=ArraySome(arr, function(value ){return value =='d';},parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=ArraySome(['a'], function(){
							echo(serialize(arguments));
 							return false;
 
                        },parallel);
		}
		assertEquals("{'1':'a','2':1,'3':['a']}",c);

		// member function
		res=arr.some(function(value ){return value =='b';},parallel);
		assertEquals(true,res);
	}


	public void function testStructSome() localMode="true" {
		_structSome(false);
	}


	public void function testStructSomeParallel() localMode="true" {
		_structSome(true);
	}

	private void function _structSome(boolean parallel) localMode="true" {
		
		sct={a:1,b:2,c:3};
		//arr[5]='e';
		
		// base test
		res=StructSome(sct, function(key,value ){return key =='b';},parallel);
		assertEquals(true,res);
		
		res=StructSome(sct, function(key,value ){return key =='d';},parallel);
		assertEquals(false,res);
		

		// closure output
		savecontent variable="c" {
			res=StructSome({a:1}, function(){
							echo(serialize(arguments));
 							return false;
 
                        },parallel);
		}
		assertEquals("{'1':'A','2':1,'3':{'A':1}}",c);

		// member function
		res=sct.some(function(key,value ){return key =='b';},parallel);
		assertEquals(true,res);
		
	}


	public void function testSome() localMode="true" {
		arr=["a","b"];
		it=arr.iterator();

		res=Some(it, function(value ){return value =='b';});
		assertEquals(true,res);
		
		it=arr.iterator();
		res=Some(it, function(value ){return value =='c';});
		assertEquals(false,res);
		
		it=arr.iterator();
		savecontent variable="c" {
			res=Some(it, function(){
							echo(serialize(arguments));
 							return false;
 
                        });
		}
		assertEquals("{'1':'a'}{'1':'b'}",c);
	}
} 
</cfscript>