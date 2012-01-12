CS=gmcs
CSFLAGS=-r:System.Web -r:Examples/Newtonsoft.Json.dll

Examples/CommandLine.exe: Examples/CommandLine.cs OAuth.cs RdioClient.cs Examples/RdioConsumerCredentials.cs
	$(CS) $(CSFLAGS) -out:$@ $^

clean:
	rm -f Examples/CommandLine.exe
