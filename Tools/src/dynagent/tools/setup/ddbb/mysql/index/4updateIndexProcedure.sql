DELIMITER $$

DROP PROCEDURE IF EXISTS `dyna2_dbo`.`updateIndex2` $$
CREATE PROCEDURE `updateIndex2`()
BEGIN

DECLARE done INT DEFAULT 0;

DECLARE idtoODT, indice int;
DECLARE valText nvarchar(100);
DECLARE i int;


DECLARE curs4 CURSOR FOR
select id_to,max(substring(val_texto, 2, length(val_texto))) from o_datos_atrib
where id_to in(select id_to from s_index)
and property=2 and isnumeric(substring(val_texto, 1, 1))=0 and isnumeric(substring(val_texto, 2, length(val_texto)))=1
group by id_to/*,substring(val_texto, 1, 1)*/
order by id_to;


DECLARE curs5 CURSOR FOR
SELECT s_index.`INDEX` FROM s_index WHERE ID_TO=idtoODT;


DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;



/*caso de catalogo, coger el maximo quitando la 1ª letra*/
SET done = 1;
OPEN curs4;
repeat
FETCH curs4 INTO idtoODT, valText;
	/*IF isnumeric(@valText)=1 AND isnumeric(@caracter)=0 */

  set i=1;
  OPEN curs5;
  while i<>0 do
  fetch curs5 into indice;
  set i = 0;
  END while;
  CLOSE curs5;

	IF indice<valText+1 then
		UPDATE S_INDEX SET `INDEX`=valText+1 WHERE ID_TO=idtoODT;
  end if;
until done END REPEAT;
CLOSE curs4;



END $$

DELIMITER ;