<div align="center">
  <h1>Unionized Villagers</h1>
  <p>The villagers have unionized.</p>
  <p>Serverside only mod.</p>
</div>

## Gameplay

Before a villager is willing to trade, they have a few demands that need accommodating or else they will decline. For example:
- They want a home for themselves that is well lit up.
- They want to be free of any disease or injuries.
- They want a village free of any hazards or monsters.
At any time you can simply strike up a conversation, and they'll let you know what's missing.

Furthermore, they want to be treated with respect. Mess with them or desecrate their village too much, and they'll go on
strike.

## Configuration

Unionized Villagers is configurable via gamerules:
- `unionized-villagers:villager_view_range`: How far away a villager can see things happening. This has an impact on performance if set too high.
- `unionized-villagers:villager_see_monsters_through_walls`: Whether villagers can see monsters through walls.
- `unionized-villagers:villager_room_minimum_size`: How many navigable blocks in a room before it is considered a viable house.

## Debug

You can set the `unionized-villagers:debug_unionized_villagers` gamerule in order to see an overview of villager needs.

You can also view and modify you and other players' strike trigger count with the `/strike_tracker` command. (Requires operator permissions)
