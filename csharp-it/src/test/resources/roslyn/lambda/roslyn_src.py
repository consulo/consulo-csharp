import re, sys
R='/run/media/vistall/repositories/roslyn/src/Compilers/CSharp/Test/Semantic/Semantics/'
def test_body(f, name):
    text=open(R+f,encoding='utf-8-sig').read()
    m=re.search(r'public (?:async Task|void) '+name+r'\(',text)
    end=text.find('\n        }\n', m.end())
    return text[m.end():end]
def sources(body):
    out=[]; i=0
    while True:
        a=body.find('"""',i); b=body.find('@"',i)
        cands=[x for x in (a,b) if x>=0]
        if not cands: return out
        k=min(cands)
        if k==a:
            e=body.find('"""',a+3)
            raw=body[a+3:e]
            lines=raw.split('\n')[1:-1]
            ind=min((len(l)-len(l.lstrip()) for l in lines if l.strip()), default=0)
            out.append('\n'.join(l[ind:] for l in lines)); i=e+3
        else:
            j=b+2; buf=[]
            while j < len(body):
                c=body[j]
                if c=='"':
                    if body[j+1:j+2]=='"': buf.append('"'); j+=2; continue
                    break
                buf.append(c); j+=1
            out.append(''.join(buf).replace('\r\n','\n')); i=j+1
            if j>=len(body): return out
if __name__=='__main__':
    f,name=sys.argv[1],sys.argv[2]
    for k,s in enumerate(sources(test_body(f,name))): print('---- source',k); print(s)
