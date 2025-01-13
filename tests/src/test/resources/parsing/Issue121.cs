public class Test
{
    public AttackResultPVPStructure GetResult()
    {
        return new AttackResultPVPStructure {
            unitDrops = GetUnitDrops(),
            buildingDamages = GetBuildingDamages(),
            battleId = BattleID,
        };
    }
}