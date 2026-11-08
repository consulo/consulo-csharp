// sdk:NET_10
#nullable enable
using System.Diagnostics.CodeAnalysis;

class Document
{
    public required string Title { get; set; }
    public required int Count;
    public string Other { get; set; } = "";
    public string Name { get; set; }

    public /*warning:CS8618*/Document()
    {
    }

    [SetsRequiredMembers]
    public Document(int count)
    {
        Title = "";
        Count = count;
        Name = "";
    }
}

class Report : Document
{
    public required string More { get; set; }
}

class Usage
{
    void Method()
    {
        var none = new /*error:CS9035*//*error:CS9035*/Document();
        var partial = new /*error:CS9035*/Document { Title = "" };
        var full = new Document { Title = "", Count = 1 };
        var sets = new Document(1);
        Document targetTyped = /*error:CS9035*/new() { Title = "" };
        var derived = new /*error:CS9035*/Report { Title = "", Count = 1 };
        var all = new Report { Title = "", Count = 1, More = "" };
    }
}
