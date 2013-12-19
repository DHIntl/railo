component {


	function init() {

		this.resources = {};

		this.mimeTypes = {

			  CSS : "text/css"
			, JS  : "text/js"
			, GIF : "image/gif"
			, JPG : "image/jpeg"
			, PNG : "image/png"

			, SVG : "image/svg+xml"
			, EOT : "application/vnd.ms-fontobject"
			, OTF : "application/x-font-opentype"
			, TTF : "application/x-font-ttf"
			, WOFF: "application/font-woff"
		};

		return this;
	}


	function onMissingTemplate( target ) {

		var filename = right( arguments.target, 4 ) == ".cfm" ? left( arguments.target, len( arguments.target ) - 4 ) : arguments.target;

		var resInfo = getResInfo( filename );

		if ( resInfo.exists ) {

			header name='Expires'       value='#getHttpTimeString( now() + 10 )#';
			header name='Cache-Control' value='max-age=#86400 * 10#';
			header name='ETag'          value=resInfo.etag;

			if ( CGI.HTTP_IF_NONE_MATCH == resInfo.etag ) {

				header statuscode='304' statustext='Not Modified';
				content reset=true type=resInfo.mimeType;
			} else {

				content reset=true type=resInfo.mimeType file=resInfo.path;
			}
		} else {
		
			header statuscode='404' statustext='Not Found';
		//	header statuscode='404' statustext='Not Found @ #resInfo.path#';

			systemOutput( "static resource #arguments.target# was not found @ #resInfo.path#", true, true );
		}

		return resInfo.exists;
	}


	private function getResInfo( filename ) {

		if ( structKeyExists( this.resources, arguments.filename ) )
			return this.resources[ arguments.filename ];

		var result = { path: expandPath( arguments.filename ) };

		result.exists = fileExists( result.path );

		if ( !result.exists )
			return result;

		var ext = listLast( arguments.filename, '.' );

		result.mimeType = this.mimeTypes.keyExists( ext ) ? this.mimeTypes[ ext ] : "";

		result.isText = left( result.mimeType, 4 ) == "text";

		result.contents = result.isText ? fileRead( result.path ) : fileReadBinary( result.path );

		result.etag = hash( result.contents );

		this.resources[ arguments.filename ] = result;

		return result;
	}

}