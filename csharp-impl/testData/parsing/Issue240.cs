public class Test
{
    private AsyncOperation async;

    IEnumerator LoadLevelSliderBad(int level)
    {
        async = Applocation.LoadLevelAsync();
    }
}