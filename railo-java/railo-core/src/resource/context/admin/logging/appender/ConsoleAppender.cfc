<cfcomponent extends="Appender">
	
    <cfset fields=array(
		field("Stream type","streamtype","output,error",true,"define if the appender logs to the error or output stream","select")
		
		)>
    
	<cffunction name="getClass" returntype="string">
    	<cfreturn "railo.commons.io.log.log4j.appender.ConsoleAppender">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string">
    	<cfreturn "Console">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Logs events to to the error or output stream">
    </cffunction>
    
</cfcomponent>