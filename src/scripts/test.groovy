//(entities('Zombie') + entities('Skeleton') + entities('Spider'))*.teleportTo(al)
['Zombie','Skeleton','Spider'].each{mob->(0..9).each{create(mob,al)}}