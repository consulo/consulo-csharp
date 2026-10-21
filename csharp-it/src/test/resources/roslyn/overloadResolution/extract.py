import re, sys, os
R='/run/media/vistall/repositories/roslyn/src/Compilers/CSharp/Test/Semantic/Semantics/'
out=sys.argv[1]
def verbatim(text, start):
    # text[start:] begins with @"
    i=start+2; buf=[]
    while True:
        c=text[i]
        if c=='"':
            if i+1<len(text) and text[i+1]=='"': buf.append('"'); i+=2; continue
            return ''.join(buf)
        buf.append(c); i+=1
for f in ['OverloadResolutionTests.cs','MethodTypeInferenceTests.cs']:
    text=open(R+f,encoding='utf-8-sig').read()
    for m in re.finditer(r'TestOverloadResolutionWithDiff\(', text):
        call=m.start()
        decls=[d for d in re.finditer(r'\n        public void (\w+)\(\)', text[:call])]
        name=decls[-1].group(1); body_start=decls[-1].end()
        region=text[body_start:call]
        sm=re.search(r'source\s*=\s*(@")', region)
        if sm and 'TestOverloadResolutionWithDiff(source' in text[call:call+50]:
            src=verbatim(text, body_start+sm.start(1))
        else:
            src=verbatim(text, text.index('@"', call))
        path=os.path.join(out, f.replace('Tests.cs','')+'_'+name+'.cs')
        open(path,'w',encoding='utf-8').write(src.replace('\r\n','\n'))
        print(os.path.basename(path), src.count('//-'))
