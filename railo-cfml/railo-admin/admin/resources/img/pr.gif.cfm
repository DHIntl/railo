<cfsavecontent variable="c">R0lGODlhFwAOAKIAAP//////AMwzAACZMwAAAAAAAAAAAAAAACH5BAAAAAAALAAAAAAXAA4AAAM3SLrcviPKIaq9V02Ju9Ub5XXgNpLEFKzsWZVDcMkuLAOAQJ/27Ao21uoH4/xeqdARGRIdH9BoAgA7</cfsavecontent><cfoutput><cfif getBaseTemplatePath() EQ getCurrentTemplatePath()><cfcontent type="image/gif" variable="#toBinary(c)#"><cfsetting showdebugoutput="no"><cfelse>data:image/gif;base64,#c#</cfif></cfoutput>