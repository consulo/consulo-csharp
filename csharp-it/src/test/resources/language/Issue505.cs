namespace HighlightError
{
    public class HighError
    {
        public static void Me()
        {
            HighError error = new HighError();

            error.Call().Trim();
            error./*error:CS0119*//*unresolved*/Call./*unresolved*/Trim();
        }

        public string Call()
        {
            return "";
        }
    }
}
